package com.warmhelp.app.services;

import com.warmhelp.app.dtos.auth.PostsRequest;
import com.warmhelp.app.dtos.auth.UpdatePostRequest;
import com.warmhelp.app.dtosResponses.*;
import com.warmhelp.app.models.Like;
import com.warmhelp.app.models.Posts;
import com.warmhelp.app.models.User;
import com.warmhelp.app.models.UserInfo;
import com.warmhelp.app.repositories.PostsRepository;
import com.warmhelp.app.repositories.UserInfoRepository;
import com.warmhelp.app.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostsService {

    private final CloudinaryService cloudinaryService;
    private final PostsRepository postsRepository;
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;

    public PostsService(CloudinaryService cloudinaryService, PostsRepository postsRepository, UserRepository userRepository, UserInfoRepository userInfoRepository) {
        this.postsRepository = postsRepository;
        this.userRepository = userRepository;
        this.userInfoRepository = userInfoRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public List<PostsResponseDTO> getAllPosts(){
        return postsRepository.findAll().stream().map(post -> {
            List<CommentsResponseDTO> commentsResponseDTOS = post.getComments().stream().map(comment -> {
                List<ResponseCommentsResponseDTO> responseDTOS = comment.getResponseComments().stream().map(response ->
                        new ResponseCommentsResponseDTO(
                                response.getId(),
                                response.getDescription(),
                                response.getUserInfo().getUser().getUsername(),
                                response.getCreatedAt(),
                                response.getUpdatedAt(),
                                response.getDeletedAt()
                        )
                ).collect(Collectors.toList());

                return new CommentsResponseDTO(
                        comment.getId(),
                        comment.getUserInfo().getUser().getUsername(),
                        comment.getDescription(),
                        responseDTOS,
                        comment.getCreatedAt(),
                        comment.getUpdatedAt(),
                        comment.getDeletedAt()
                );

            }).collect(Collectors.toList());

            List<ReportPostDTO> reports = post.getReportPosts().stream().map(reportPost ->
                    new ReportPostDTO(
                            reportPost.getId(),
                            reportPost.getDescription(),
                            reportPost.getType().getReportType().name(),
                            reportPost.getState().getReportState().name(),
                            reportPost.getUserInfo().getUser().getUsername(),
                            reportPost.getPost().getId(),
                            reportPost.getPost().getTitle(),
                            reportPost.getPost().getDescription(),
                            reportPost.getCreatedAt(),
                            reportPost.getUpdatedAt(),
                            reportPost.getDeletedAt()
                    )).collect(Collectors.toList());

            List<Likes_Posts_ResponseDTO> likes_posts = post.getLikesPosts().stream().map(likes->
                    new Likes_Posts_ResponseDTO(
                            likes.getUserInfo().getUser().getUsername(),
                            likes.getCreatedAt()
                    )).collect(Collectors.toList());


            return new PostsResponseDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getUserInfo().getUser().getUsername(),
                    post.getDescription(),
                    post.getImage(),
                    commentsResponseDTOS,
                    reports,
                    likes_posts,
                    post.getCreatedAt(),
                    post.getUpdatedAt(),
                    post.getDeletedAt(),
                    post.getUserInfo().getAvatar()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deletePostById(Long id){
        Posts post = postsRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("post not found"));

        post.getLikesPosts().forEach(like -> {});

        post.getComments().forEach(comments -> {
            comments.getResponseComments().forEach(responseComments -> {});
        });

        post.getReportPosts().forEach(reportPost -> {});

        postsRepository.delete(post);
    }


    @Transactional
    public Posts createPost(PostsRequest postFormFront){
        UserInfo userInfo = this.userInfoRepository.findByUser_Username(postFormFront.getUserName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if(this.postsRepository.existsByTitle(postFormFront.getTitle())){
            throw new IllegalArgumentException("Post ya existe");
        }

        Posts post = new Posts();
        post.setTitle(postFormFront.getTitle());
        post.setDescription(postFormFront.getDescription());
        post.setImage(postFormFront.getImage());
        post.setUserInfo(userInfo);

        return this.postsRepository.save(post);
    }

    public ResponseEntity<?> createPostWithImage(String title, String description, String userName, MultipartFile image){
        UserInfo userInfo = this.userInfoRepository.findByUser_Username(userName)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if(this.postsRepository.existsByTitle(title)){
            throw new IllegalArgumentException("Post ya existe");
        }

        try {
            String imageUrl = null;

            if (image != null && !image.isEmpty()) {
                // ✅ Subida a Cloudinary
                imageUrl = cloudinaryService.uploadPostFile(image);
            }

            Posts post = new Posts();
            post.setTitle(title);
            post.setDescription(description);
            post.setImage(imageUrl); // ✅ Guardamos URL de Cloudinary
            post.setUserInfo(userInfo);

            postsRepository.save(post);
            return ResponseEntity.ok(post); // Devolvemos el post con URL lista
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al subir imagen: " + e.getMessage());
        }
    }


    @Transactional
    public Posts updatePost(Long id, UpdatePostRequest request){
        Posts post = postsRepository.findById(id).orElseThrow(()->new IllegalArgumentException("post not found"));
        if(!post.getTitle().equals(request.getTitle())){
            post.setTitle(request.getTitle());
        }

        if (!post.getDescription().equals(request.getDescription())){
            post.setDescription(request.getDescription());
        }

        if (request.getImage() != null && !request.getImage().equals(post.getImage())) {
            post.setImage(request.getImage());
        }

        postsRepository.save(post);
        return post;
    }

}
