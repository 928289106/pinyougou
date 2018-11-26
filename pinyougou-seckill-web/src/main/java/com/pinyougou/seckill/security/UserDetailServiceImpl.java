package com.pinyougou.seckill.security;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //这个是只是做 授权 认证交给CAS server
        return new User(s,"", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
    }
}
