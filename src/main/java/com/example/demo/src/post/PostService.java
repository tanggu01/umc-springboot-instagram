package com.example.demo.src.post;


import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.post.model.*;
import com.example.demo.src.user.model.PatchUserReq;
//import com.example.demo.utils.AES128;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Service
public class PostService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PostDao postDao;
    private final PostProvider postProvider;
    private final JwtService jwtService;


    @Autowired
    public PostService(PostDao postDao, PostProvider postProvider, JwtService jwtService) {
        this.postDao = postDao;
        this.postProvider = postProvider;
        this.jwtService = jwtService;

    }

    public PostPostsRes createPosts (int userIdx, PostPostsReq postPostsReq) throws BaseException{
        try {
            //Dao에서 개발을 편리하기위해 postIdx 값을 바로 받아준다
            //insertPosts 는 게시물의 내용만 넣어준다.
            int postIdx = postDao.insertPosts(userIdx, postPostsReq.getContent());
            //이미지는 리스트로 넣어줘야함: 반복문을 사용해 다른 함수로 처리해준다 (insertPostImgs)
            for (int i=0; i<postPostsReq.getPostImgUrls().size(); i++) {
                postDao.insertPostImgs(postIdx, postPostsReq.getPostImgUrls().get(i));
            }
            //!!! postIdx 를 받아줬기 때문에 이 객체에 postIdx를 넣어서 리턴해준다. !!!
            return new PostPostsRes(postIdx);
        }
        catch (Exception exception) {
            System.out.println(exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void modifyPost(int userIdx, int postIdx, PatchPostsReq patchPostsReq) throws BaseException {
        //있는 유저인지 먼저 확인 - from Provider (checkUserExist)
        if (postProvider.checkUserExist(userIdx) == 0) {
            throw new BaseException(USERS_EMPTY_USER_ID);
        }
        if (postProvider.checkPostExist(postIdx) == 0) {
            throw new BaseException(POSTS_EMPTY_POST_ID);
        }
        try {
            //Dao 에서는 result 값을 받아준다.
            //postDao 에서 UPDATE 문이 잘 실행 되면 result 로 1을 받고 아니면 0을 받아 에러코드를 전달해준다.
            //Dao 에서 result 가 0으로 오면 디비 접근하는데 오류가 있거나 데이터가 정상적으로 들어가지 않았다는 뜻
            int result = postDao.updatePost(postIdx, patchPostsReq.getContent()); //네이밍 updatePosts: 쿼리문이 update이기 때문
            if (result == 0) {
                throw new BaseException(MODIFY_FAIL_POST);
            }
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //3.4 게시물 삭제 API
    public void deletePost(int postIdx) throws BaseException {
        if (postProvider.checkPostExist(postIdx) == 0) {
            throw new BaseException(POSTS_EMPTY_POST_ID);
        }
        try {
            int result = postDao.deletePost(postIdx);
            if (result == 0) {
                throw new BaseException(DELETE_FAIL_POST);
            }
        } catch (BaseException exception){
            throw new BaseException(exception.getStatus());
        }
    }
}