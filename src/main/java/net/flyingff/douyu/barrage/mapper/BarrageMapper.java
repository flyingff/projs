package net.flyingff.douyu.barrage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import net.flyingff.douyu.barrage.bean.Barrage;

public interface BarrageMapper {
	@Select("select * from barrage")
	List<Barrage> selectAll();
	
	@Select("select * from barrage where time >= #{0} and time <= #{1}")
	List<Barrage> selectByTime(long start, long end);
	
	@Select("select * from barrage where user_name == #{0} ")
	List<Barrage> selectByName(String uName);
	
	@Insert("insert into barrage(`user_name`, ``) values()")
	void insert(Barrage b);
	
}
