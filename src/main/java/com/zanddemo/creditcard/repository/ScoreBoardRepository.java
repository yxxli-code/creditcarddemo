package com.zanddemo.creditcard.repository;

import com.zanddemo.creditcard.entity.ScoreBoard;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreBoardRepository {

	void add(ScoreBoard scoreBoard);

	ScoreBoard findById(@Param("emiratesId") String emiratesId);

	void update(ScoreBoard scoreBoard);

	void reset(ScoreBoard scoreBoard);
}
