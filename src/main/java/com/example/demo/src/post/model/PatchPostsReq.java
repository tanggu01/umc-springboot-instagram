package com.example.demo.src.post.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor

//이미지 말고 내용만 수정 가능
//어떤 유저가 수정할건지, 어떤내용 수정할건지
public class PatchPostsReq {
    private int userIdx; //jwt or path variable
    private String content;
//    private List<PostImgUrlReq> postImgUrls;


}
