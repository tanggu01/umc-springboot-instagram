package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.config.BaseResponseStatus;
import com.example.demo.src.post.model.*;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/posts")
public class PostController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final PostProvider postProvider;
    @Autowired
    private final PostService postService;
    @Autowired
    private final JwtService jwtService;


    public PostController(PostProvider postProvider, PostService postService, JwtService jwtService){
        this.postProvider = postProvider;
        this.postService = postService;
        this.jwtService = jwtService;
    }

    //2.1 유저 피드 조회 API
    @ResponseBody
    @GetMapping("") // (GET) 127.0.0.1:9000/posts
    public BaseResponse<List<GetPostsRes>> getPosts(@RequestParam int userIdx) { //<GetUserRes> model: 응답값.
        //Model에서는 필요한 요청값/응답값 형식을 정리해놓는다. 어떠한 형태/어떠한 데이터를 출력할건지/클라에게 전달할건지 정의를 해주는곳
        try {
            List<GetPostsRes> getPostsRes = postProvider.retrievePosts(userIdx);
            return new BaseResponse<>(getPostsRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //3.2 POST 게시물 생성 api : /post
    @ResponseBody
    @PostMapping("") // Post이기때문에 body가 필요하다, 받은 응답값을 body로 전달
    public BaseResponse<PostPostsRes> createPosts(@RequestBody PostPostsReq postPostsReq) { //<PostpostRes> model: 응답값. 만든 게시물의 idx값을 리ㄷ
        try {
            //형식적 validation
            if (postPostsReq.getContent().length() > 450) { //이미지, 게시글에대한것 (길이제한)
                return new BaseResponse<>(BaseResponseStatus.POST_POSTS_INVALID_CONTENTS);
            }
            if (postPostsReq.getPostImgUrls().size() < 1) {
                return new BaseResponse<>(BaseResponseStatus.POST_POSTS_EMPTY_IMGURL);
            }
            //userIdx 빼는이유: jwt로 따로 받아서 사용할수 있기때문, 그때 편리함 위해
            PostPostsRes postPostsRes = postService.createPosts(postPostsReq.getUserIdx(), postPostsReq); //따로 userIdx를 받고잇지않아 body값에서 idx 빼주기
            return new BaseResponse<>(postPostsRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //3.3 PATCH 게시물 수정 API
    @ResponseBody
    @PatchMapping("/{postIdx}") // path variable로
    public BaseResponse<String> modifyPost (@PathVariable ("postIdx") int postIdx,  @RequestBody PatchPostsReq patchPostsReq) { //model 말고 문자열 출력
        try {
            //형식적 validation
            if (patchPostsReq.getContent().length() > 450) { //게시글에대한것 (길이제한)
                return new BaseResponse<>(BaseResponseStatus.POST_POSTS_INVALID_CONTENTS);
            }
            //뭘 리턴하는게 아니라 그냥 postService에 갔다가 오류 안나면 result 출력하는 로직
            postService.modifyPost(patchPostsReq.getUserIdx(), postIdx, patchPostsReq); //userIdx, path variable로 받은 postIdx, Body 값
            String result = "게시물 수정을 완료했습니다.";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //3.4 게시물 삭제 API  - 삭제이기때문에 데이터를 받아올필요가 없다 - 모델 만들필요 x
    @ResponseBody
    @PatchMapping("/{postIdx}/status") // path variable로
    public BaseResponse<String> deletePost (@PathVariable ("postIdx") int postIdx) { //model 말고 문자열 출력
        try {
            //뭘 리턴하는게 아니라 그냥 postService에 갔다가 오류 안나면 result 출력하는 로직
            postService.deletePost(postIdx); //userIdx, path variable로 받은 postIdx, Body 값
            String result = "게시물 삭제에 성공했습니다.";
           return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }



}





