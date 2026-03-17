package ch.hftm.blog.boundary;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ch.hftm.blog.control.BlogService;
import ch.hftm.blog.entity.Blog;
import ch.hftm.blog.entity.BlogStatus;
import ch.hftm.blog.messaging.ValidationRequest;
import ch.hftm.blog.messaging.ValidationRequestProducer;
import io.quarkus.logging.Log;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Tag(name = "Blog")
@Path("blogs")
public class BlogResource {

    @Inject
    BlogService blogService;
    @Inject
    ValidationRequestProducer producer;

    @POST
    public Response create(Blog blog) {
        Log.infof("POST /blogs - creating blog with title='%s'", blog != null ? blog.title : null);

        Blog created = blogService.create(blog);

        Log.infof("POST /blogs - created blog id=%d with status=%s", created.id, created.status);
        Log.infof("POST /blogs - sending validation request for blog id=%d", created.id);

        producer.send(new ValidationRequest("BLOG", created.id, created.content));
        return Response.status(201).entity(created).build();
    }

    @GET
    public List<Blog> listApproved() {
        Log.info("GET /blogs - listing approved blogs");
        return Blog.list("status", BlogStatus.APPROVED);
    }

    @GET
    @Path("{id}")
    @PermitAll
    public Response getBlog(@PathParam("id") long id) {

        Log.infof("GET /blogs/%d", id);

        Blog blog = blogService.getBlog(id);
        if (blog == null) {
            Log.warnf("Blog with id=%d not found", id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Log.infof("Returning blog with id=%d and status=%s", id, blog.status);
        return Response.ok(blog).build();
    }

    @PATCH
    @Path("{id}")
    @RolesAllowed("author")
    public Response changeBlog(@PathParam("id") long id, Blog updatedBlog) {

        Log.infof("PATCH /blogs/%d - updating blog", id);

        Blog updated = blogService.updateBlog(id, updatedBlog);

        if (updated == null) {
            Log.warnf("Blog with id=%d not found for update", id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Log.infof("Blog with id=%d updated and reset to status=%s", id, updated.status);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/delete/{id}")
    @RolesAllowed({ "author", "admin" })
    @APIResponse(responseCode = "204", description = "Deleted")
    public Response deleteBlogById(@PathParam("id") Long id) {

        Log.infof("DELETE /blogs/delete/%d", id);

        boolean deleted = blogService.deleteBlogById(id);
        if (!deleted) {
            Log.warnf("Blog with id=%d not found for deletion", id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Log.infof("Blog with id=%d deleted", id);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/delete")
    @RolesAllowed("admin")
    @APIResponse(responseCode = "204", description = "All blogs deleted")
    public Response deleteAllBlogs() {

        Log.warn("DELETE /blogs/delete - deleting ALL blogs");

        blogService.deleteBlogs();

        Log.info("All blogs deleted");
        return Response.noContent().build();
    }
}
