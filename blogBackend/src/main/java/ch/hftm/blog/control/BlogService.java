package ch.hftm.blog.control;

import java.util.List;
import java.util.Optional;

import ch.hftm.blog.entity.Blog;
import ch.hftm.blog.entity.BlogStatus;
import ch.hftm.blog.metrics.BlogMetrics;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class BlogService {

    @Inject
    BlogRepository blogRepository;

    @Inject
    BlogMetrics blogMetrics;

    @Transactional
    public Blog create(Blog blog) {
        Log.info("Persisting new blog and setting status to PENDING");

        blog.status = BlogStatus.PENDING;
        blog.persist();
        blogMetrics.incrementBlogCreated();

        Log.infof("Blog persisted with id=%d", blog.id);
        return blog;
    }

    @Transactional
    public void applyValidationResult(Long blogId, boolean approved, String reason) {
        Log.infof("Applying validation result for blog id=%d: approved=%s, reason=%s", blogId, approved, reason);

        Blog blog = blogRepository.findById(blogId);
        if (blog == null) {
            Log.warnf("Validation result received for unknown blog id=%d", blogId);
            return;
        }

        blog.status = approved ? BlogStatus.APPROVED : BlogStatus.REJECTED;

        if (approved) {
            blogMetrics.incrementApproved();
        } else {
            blogMetrics.incrementRejected();
        }

        Log.infof("Blog id=%d status updated to %s", blogId, blog.status);
    }

    public Blog getBlog(long id) {
        return Blog.findById(id);
    }

    public Blog updateBlog(long id, Blog updated) {
        Blog blog = Blog.findById(id);
        if (blog == null)
            return null;

        blog.title = updated.title;
        blog.content = updated.content;
        blog.status = BlogStatus.PENDING;

        return blog;
    }

    public boolean deleteBlogById(Long id) {
        return Blog.deleteById(id);
    }

    public void deleteBlogs() {
        Blog.deleteAll();
    }

    public List<Blog> getBlogs(Optional<String> search, Optional<Integer> page, Optional<Integer> size) {

        var query = Blog.find("lower(title) like ?1", "%" + search.orElse("").toLowerCase() + "%");

        if (page.isPresent() && size.isPresent()) {
            query.page(page.get(), size.get());
        }

        return query.list();
    }

}
