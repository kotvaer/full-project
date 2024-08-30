package dev.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.example.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
