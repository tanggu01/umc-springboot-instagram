package com.example.demo.src.post;


import com.example.demo.src.post.model.*;
import com.example.demo.src.user.model.GetUserPostsRes;
import com.example.demo.src.user.model.PatchUserReq;
import com.example.demo.src.user.model.PostUserReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PostDao {

    private JdbcTemplate jdbcTemplate;
    private List<GetPostImgRes> getPostImgRes;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetPostsRes> selectPosts(int userIdx){
        String selectPostsQuery = "\n" +
                "        SELECT p.postIdx as postIdx,\n" +
                "            u.userIdx as userIdx,\n" +
                "            u.nickName as nickName,\n" +
                "            u.profileImgUrl as profileImgUrl,\n" +
                "            p.content as content,\n" +
                "            IF(postLikeCount is null, 0, postLikeCount) as postLikeCount,\n" +
                "            IF(commentCount is null, 0, commentCount) as commentCount,\n" +
                "            case\n" +
                "                when timestampdiff(second, p.updatedAt, current_timestamp) < 60\n" +
                "                    then concat(timestampdiff(second, p.updatedAt, current_timestamp), '초 전')\n" +
                "                when timestampdiff(minute , p.updatedAt, current_timestamp) < 60\n" +
                "                    then concat(timestampdiff(minute, p.updatedAt, current_timestamp), '분 전')\n" +
                "                when timestampdiff(hour , p.updatedAt, current_timestamp) < 24\n" +
                "                    then concat(timestampdiff(hour, p.updatedAt, current_timestamp), '시간 전')\n" +
                "                when timestampdiff(day , p.updatedAt, current_timestamp) < 365\n" +
                "                    then concat(timestampdiff(day, p.updatedAt, current_timestamp), '일 전')\n" +
                "                else timestampdiff(year , p.updatedAt, current_timestamp)\n" +
                "            end as updatedAt,\n" +
                "            IF(pl.status = 'ACTIVE', 'Y', 'N') as likeOrNot\n" +
                "        FROM Post as p\n" +
                "            join User as u on u.userIdx = p.userIdx\n" +
                "            left join (select postIdx, userIdx, count(likeIdx) as postLikeCount from PostLike WHERE status = 'ACTIVE' group by postIdx) plc on plc.postIdx = p.postIdx\n" +
                "            left join (select postIdx, count(commentIdx) as commentCount from Comment WHERE status = 'ACTIVE' group by postIdx) c on c.postIdx = p.postIdx\n" +
                "            left join Follow as f on f.followeeIdx = p.userIdx and f.status = 'ACTIVE'\n" +
                "            left join PostLike as pl on pl.userIdx = f.followerIdx and pl.postIdx = p.postIdx\n" +
                "        WHERE f.followerIdx = ? and p.status = 'ACTIVE'\n" +
                "        group by p.postIdx;\n" ;

        int selectPostsParam = userIdx;
        return this.jdbcTemplate.query(selectPostsQuery,
                (rs,rowNum) -> new GetPostsRes(
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("nickName"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content"),
                        rs.getInt("postLikeCount"),
                        rs.getInt("commentCount"),
                        rs.getString("updatedAt"),
                        rs.getString("likeOrNot"),
                        getPostImgRes = this.jdbcTemplate.query(
                                "SELECT pi.postImgUrlIdx,  pi.imgUrl\n" +
                                "        FROM PostImgUrl as pi\n" +
                                "            join Post as p on p.postIdx = pi.postIdx\n" +
                                "        WHERE pi.status = 'ACTIVE' and p.postIdx = ?;\n",
                                (rk,rownum) -> new GetPostImgRes(
                                        rk.getInt("postImgUrlIdx"),
                                        rk.getString("imgUrl")
                                ),rs.getInt("postIdx") // ?에 들어가야할 parameter: 위에서 가져온 getInt(postIdx)
                        )
                ),selectPostsParam);
    }

    public int checkUserExist(int userIdx){ //존재하는지
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);
    }

    //3.3 / 3.4 게시물 존재하는지 의미적 validation
    public int checkPostExist(int postIdx){ //존재하는지
        String checkPostExistQuery = "select exists(select postIdx from Post where postIdx = ?)";
        int checkPostExistParams = postIdx;
        return this.jdbcTemplate.queryForObject(checkPostExistQuery,
                int.class,
                checkPostExistParams);
    }


    //3.2 게시물 포스트 올리는 함수
    public int insertPosts(int userIdx, String content) { //함수 호출시 데이터를 넣어줌.
        String insertPostQuery = "INSERT INTO Post(userIdx, content) VALUES (?,?)";
        //물음표에 들어갈 변수 받기
        Object[] insertPostParams = new Object[]{userIdx, content};

        //insert를 사용할때는 return 말고 업데이트를 해줘야함
        this.jdbcTemplate.update(insertPostQuery, insertPostParams);

        //방금 넣음 Post의 idx를 클라에게 전달해야함! 자동으로 가장 마지막에 들어간 idx값 리턴
        String lastInsertIdxQuery = "select last_insert_id()";
        //쿼리문 실행해주는 구문
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery,int.class); //int.class : 쿼리문에 대한 반환 타입
    }

    //이미지 넣어주는 함수 3.2 Post 게시물 생성 api
    public int insertPostImgs(int postIdx, PostImgUrlReq postImgUrlReq) { //함수 호출시 데이터를 넣어줌.
        String insertPostImgsQuery = "INSERT INTO PostImgUrl(postIdx, imgUrl) VALUES (?,?)";
        //물음표에 들어갈 변수 받기
        Object[] insertPostImgsParams = new Object[]{postIdx, postImgUrlReq.getImgUrl()};

        //insert를 사용할때는 return 말고 업데이트를 해줘야함
        this.jdbcTemplate.update(insertPostImgsQuery, insertPostImgsParams);

        //방금 넣음 PostImg의 idx를 클라에게 전달해야함! 자동으로 가장 마지막에 들어간 idx값 리턴
        String lastInsertIdxQuery = "select last_insert_id()";
        //쿼리문 실행해주는 구문
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery,int.class); //int.class : 쿼리문에 대한 반환 타입
    }


    //3.3 Patch 게시물 수정 api
    public int updatePost(int postIdx, String content) {
        String updatePostQuery = "UPDATE Post SET content=? WHERE postIdx=? ";
        Object[] updatePostParams = new Object[]{content, postIdx}; //물음표에 들어갈 변수 받기
        return this.jdbcTemplate.update(updatePostQuery, updatePostParams);  //return 0 or 1

    }

    //3.4 Patch 게시물 삭제 api
    public int deletePost(int postIdx) {
        String deletePostQuery = "UPDATE Post SET status='INACTIVE' WHERE postIdx=? ";
        Object[] deletePostParams = new Object[]{postIdx};
        return this.jdbcTemplate.update(deletePostQuery,deletePostParams);
    }
}

