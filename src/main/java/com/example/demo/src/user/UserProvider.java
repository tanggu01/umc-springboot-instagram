package com.example.demo.src.user;


import com.example.demo.config.BaseException;
import com.example.demo.src.user.model.GetUserFeedRes;
import com.example.demo.src.user.model.GetUserInfoRes;
import com.example.demo.src.user.model.GetUserPostsRes;
import com.example.demo.src.user.model.GetUserRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;
import static com.example.demo.config.BaseResponseStatus.USERS_EMPTY_USER_ID;

//Provider : Read의 비즈니스 로직 처리
@Service
public class UserProvider {

    private final UserDao userDao;
    private final JwtService jwtService;


    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserProvider(UserDao userDao, JwtService jwtService) {
        this.userDao = userDao;
        this.jwtService = jwtService;
    }


    public GetUserFeedRes retriveUserFeed (int userIdxByJwt, int userIdx) throws BaseException{
        boolean isMyFeed = true;

        if(checkUserExist(userIdx) == 0){
            throw new BaseException(USERS_EMPTY_USER_ID);
        }
        try {
            if(userIdxByJwt != userIdx)
                isMyFeed = false;

            GetUserInfoRes getUserInfo = userDao.selectUserInfo(userIdx); //유저의 정보를 받아오는 객체
            List<GetUserPostsRes> getUserPosts = userDao.selectUserPosts(userIdx);
            //유저 피트 하나를 만들어 ismyFeed, getUSerInfoRes, postRes 를 넣어줌
            GetUserFeedRes getUserRes = new GetUserFeedRes(isMyFeed,getUserInfo,getUserPosts);
            return getUserRes;
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


//    public GetUserFeedRes getUsersByIdx(int userIdx) throws BaseException{
//        try{
//            GetUserFeedRes getUsersRes = userDao.getUsersByIdx(userIdx);
//            return getUsersRes;
//        }
//        catch (Exception exception) {
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }


    //Week 7 /user/?Email=
    public GetUserRes getUsersByEmail(String email) throws BaseException{
        try{
            GetUserRes getUsersRes = userDao.getUsersByEmail(email);
            return getUsersRes;
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //Week 7 /user/{userIdx}
    public GetUserRes getUsersByIdx(int userIdx) throws BaseException{
        try{
            GetUserRes getUsersRes = userDao.getUsersByIdx(userIdx);
            return getUsersRes;
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    public int checkEmail(String email) throws BaseException{
        try{
            return userDao.checkEmail(email);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //7주차 챌린지 [DELETE]
    public int checkUser(int userIdx) throws BaseException{
        try {
            return userDao.checkUser(userIdx);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //8주차 
    public int checkUserExist(int userIdx) throws BaseException{
        try{
            return userDao.checkUserExist(userIdx);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
