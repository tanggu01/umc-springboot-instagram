package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;
//유저 도메인
@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;


    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService) {
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }


    /** Week 7
     * 회원 조회 API   //Query String - @RequestParam
     * [GET] /users
     * 이메일 검색 조회 API
     * [GET] /users? Email=
     * @return BaseResponse<GetUserRes>
     */
    @ResponseBody
    @GetMapping("") // (GET) 127.0.0.1:9000/users
    public BaseResponse<GetUserRes> getUsers(@RequestParam(required = true) String Email) { //<GetUserRes> model: 응답값.
        //Model에서는 필요한 요청값/응답값 형식을 정리해놓는다. 어떠한 형태/어떠한 데이터를 출력할건지/클라에게 전달할건지 정의를 해주는곳
        try {
            // TODO: email 관련한 짧은 validation 예시입니다. 그 외 더 부가적으로 추가해주세요!
            if (Email.length() == 0) {
                return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
            }
            // 이메일 정규표현
            if (!isRegexEmail(Email)) {
                return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
            }
            GetUserRes getUsersRes = userProvider.getUsersByEmail(Email);
            return new BaseResponse<>(getUsersRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    //유저 조회 API
    @ResponseBody
    @GetMapping("/{userIdx}") // (GET) 127.0.0.1:9000/users/:userIdx
    public BaseResponse<GetUserRes> getUserByIdx(@PathVariable("userIdx") int userIdx) {
        try {
            GetUserRes getUserRes = userProvider.getUsersByIdx(userIdx);
            return new BaseResponse<>(getUserRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 회원가입 API
     * [POST] /users
     * @return BaseResponse<PostUserRes>
     */
    // Body
    @ResponseBody
    @PostMapping("") // (POST) 127.0.0.1:9000/users
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq) {
        // TODO: email 관련한 짧은 validation 예시입니다. 그 외 더 부가적으로 추가해주세요!
        if (postUserReq.getEmail() == null) {
            return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
        }
        // 이메일 정규표현
        if (!isRegexEmail(postUserReq.getEmail())) {
            return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
        }
        try {
            PostUserRes postUserRes = userService.createUser(postUserReq);
            return new BaseResponse<>(postUserRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 유저정보변경 API
     * [PATCH] /users/:userIdx
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userIdx}") // (PATCH) 127.0.0.1:9000/users/:userIdx
    public BaseResponse<String> modifyUserName(@PathVariable("userIdx") int userIdx, @RequestBody User user) {
        try {
            /* TODO: jwt는 다음주차에서 배울 내용입니다!
            jwt에서 idx 추출.
            int userIdxByJwt = jwtService.getUserIdx();
            userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            */
            PatchUserReq patchUserReq = new PatchUserReq(userIdx, user.getNickName());
            userService.modifyUserName(patchUserReq);

            String result = "";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //Week 7 Challenge - 유저 삭제 API: status 만 inactive 로 바꿔주기. [PATCH]
    // 삭제이기때문에 데이터를 받아올필요가 없다 - 모델 만들필요 x
    @ResponseBody
    @PatchMapping("/{userIdx}/status")
    public BaseResponse<String> deleteUser(@PathVariable("userIdx") int userIdx) { //클라이언트에게 보내줄정보: model 말고 문자열
        try {
            //뭘 리턴하는게 아니라 그냥 postService에 갔다가 오류 안나면 result 출력하는 로직
            userService.deleteUser(userIdx);
            String result = "유저가 삭제되었습니다.";
            return new BaseResponse<>(result); //이 Result는 어디에서 온거지?

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //유저삭제 2 - [DELETE]
    @ResponseBody
    @DeleteMapping("/{userIdx}") // (DELETE) 127.0.0.1:9000/users/:userIdx
    public BaseResponse<String> deleteUserByIdx(@PathVariable("userIdx") int userIdx) {
        try {
            userService.deleteUserbyIdx(userIdx); //delete니까 service
            String result = " 유저가 삭제되었습니다."; //리턴 <String>
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //Week 8
    //유저 피드 조회 API
    @ResponseBody
    @GetMapping("/{userIdx}/feed") // (GET) 127.0.0.1:9000/users
    public BaseResponse<GetUserFeedRes> getUsersFeed(@PathVariable("userIdx") int userIdx) { //<GetUserRes> model: 응답값.
        //Model에서는 필요한 요청값/응답값 형식을 정리해놓는다. 어떠한 형태/어떠한 데이터를 출력할건지/클라에게 전달할건지 정의를 해주는곳
        try {
            GetUserFeedRes getUserFeedRes = userProvider.retriveUserFeed(userIdx,userIdx);
            return new BaseResponse<>(getUserFeedRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


}
