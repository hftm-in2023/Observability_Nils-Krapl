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
        Blog created = blogService.create(blog); // TX endet nach Rückgabe
        producer.send(new ValidationRequest("BLOG", created.id, created.content)); // OUTSIDE TX
        return Response.status(201).entity(created).build();
    }

    @GET
    public List<Blog> listApproved() {
        return Blog.list("status", BlogStatus.APPROVED);
    }

    @GET
    @Path("{id}")
    @PermitAll
    public Response getBlog(@PathParam("id") long id) {

        Log.infof("GET /blog/%d", id);

        Blog blog = blogService.getBlog(id);
        if (blog == null) {
            Log.warnf("Blog with id=%d not found", id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(blog).build();
    }

    @PATCH
    @Path("{id}")
    @RolesAllowed("author")
    public Response changeBlog(@PathParam("id") long id, Blog updatedBlog) {

        Log.infof("PATCH /blog/%d - updating blog", id);

        Blog updated = blogService.updateBlog(id, updatedBlog);

        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/delete/{id}")
    @RolesAllowed({ "author", "admin" })
    @APIResponse(responseCode = "204", description = "Deleted")
    public Response deleteBlogById(@PathParam("id") Long id) {

        Log.infof("DELETE /blog/delete/%d", id);

        boolean deleted = blogService.deleteBlogById(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.noContent().build();
    }

    @DELETE
    @Path("/delete")
    @RolesAllowed("admin")
    @APIResponse(responseCode = "204", description = "All blogs deleted")
    public Response deleteAllBlogs() {

        Log.warn("DELETE /blog/delete - deleting ALL blogs!");

        blogService.deleteBlogs();

        return Response.noContent().build();
    }

    // @GET
    // @PermitAll
    // public List<Blog> getBlogs(@QueryParam("search") Optional<String>
    // searchString,
    // @QueryParam("page") Optional<Integer> page,
    // @QueryParam("size") Optional<Integer> size) {
    // Log.infof("GET /blog - search=%s, page=%s, size=%s",
    // searchString.orElse("none"),
    // page.map(Object::toString).orElse("none"),
    // size.map(Object::toString).orElse("none"));
    // return blogService.getBlogs(searchString, page, size);
    // }

    // @GET
    // @Path("{id}")
    // @PermitAll
    // public Response getBlog(@PathParam("id") long id) {
    // Log.infof("GET /blog/%d", id);
    // Blog blog = this.blogService.getBlog(id);
    // if (blog == null) {
    // Log.warnf("Blog with id=%d not found", id);
    // return Response.status(Response.Status.NOT_FOUND).build();
    // }
    // Log.infof("Returning blog with id=%d", id);
    // return Response.ok(blog).build();
    // }

    // @POST
    // @APIResponse(responseCode = "201", description = "Created")
    // @RequestBody(content = @Content(example = "{\"title\": \"string\"}"))
    // @RolesAllowed("author")
    // public Response addBlog(Blog blog) {
    // Log.infof("POST /blog - creating blog with title='%s'", blog.getTitle());
    // this.blogService.addBlog(blog);
    // Log.infof("Blog created with title='%s'", blog.getTitle());
    // return Response.status(Response.Status.CREATED).entity(blog).build();
    // }

    // @PATCH
    // @Path("{id}")
    // @RolesAllowed("author")
    // public void changeBlog(@PathParam("id") long id, Blog updatedBlog) {
    // Log.infof("PATCH /blog/%d - updating blog", id);
    // this.blogService.updateBlog(id, updatedBlog);
    // Log.infof("Blog %d updated", id);
    // }

    // @DELETE
    // @Path("/delete/{id}")
    // @APIResponse(responseCode = "204", description = "Deleted")
    // @RolesAllowed("author, admin")
    // public void deleteBlogById(@PathParam("id") Long id) {
    // Log.infof("DELETE /blog/delete/%d", id);
    // blogService.deleteBlogById(id);
    // Log.infof("Blog %d deleted", id);
    // }

    // @DELETE
    // @Path("/delete")
    // @APIResponse(responseCode = "204", description = "All blogs deleted")
    // @RolesAllowed("admin")
    // public void deleteAllBlogs() {
    // Log.warn("DELETE /blog/delete - deleting ALL blogs!");
    // blogService.deleteBlogs();
    // Log.info("All blogs deleted");
    // }
}
