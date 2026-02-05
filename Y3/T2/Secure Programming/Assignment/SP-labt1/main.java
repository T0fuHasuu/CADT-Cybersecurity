package com.burger.service.impl;

import com.burger.constant.Role;
import com.burger.model.dto.ChangePasswordDto;
import com.burger.model.dto.LimiterPageDto;
import com.burger.model.dto.UserDto;
import com.burger.model.dto.UserMailDto;
import com.burger.model.dto.UserRegistrationDto;
import com.burger.model.entity.RoleForUsers;
import com.burger.model.entity.User;
import com.burger.model.mapper.LimiterPageDtoMapper;
import com.burger.model.mapper.UserMapper;
import com.burger.repository.RoleForUsersRepository;
import com.burger.repository.UserRepository;
import com.burger.security.jwt.details.JwtUserDetails;
import com.burger.service.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javassist.NotFoundException;

import javax.validation.constraints.NotNull;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleForUsersRepository roleRepository;
    private final LimiterPageDtoMapper limiterPageDtoMapper;


    @Override
    public UserDto register(UserRegistrationDto userRegistrationDto) {
        if (userRepository.findByUsername(
                userRegistrationDto.getUsername()).isPresent()) {
            log.info(" error registration ");
            return new UserDto();
        }

        User user = fillToRegisteredUser(userRegistrationDto);

        User registeredUser = userRepository.save(user);
        log.info("IN register - user: {} successfully registered",
                registeredUser.getId());

        return userMapper.toUserDto(registeredUser);
    }

    private User fillToRegisteredUser(UserRegistrationDto userRegistrationDto) {
        User user = userMapper.toUser(userRegistrationDto);
        Optional<RoleForUsers> roleForUsers =
                roleRepository.findByRoleName(Role.ROLE_USER.toString());
        user.setRoles(Collections.singletonList(roleForUsers.get()));
        user.setEnable(true);
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));
        return user;
    }

    @Override
    public Optional<User> getUserByContext() {
        return Optional.of(userRepository.findByUsername(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName())).
                orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public JwtUserDetails getContextUser() {
        return (JwtUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getCredentials();
    }

    @Override
    public List<UserDto> getAll(LimiterPageDto limiterPageDto) {

        Pageable pageable = limiterPageDtoMapper.toPageable(limiterPageDto);
        List<UserDto> userDtos =
                userMapper.toListUserDto(
                        userRepository.findAll(pageable).toList());

        log.info(getContextUser().getId() + " requested Users ");
        return userDtos;
    }

    @Override
    public UserDto findByUsername(String username) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(() ->
                    new UsernameNotFoundException(username));
            return userMapper.toUserDto(user);
        } catch (UsernameNotFoundException e) {
            log.error("User " + username + " not found");
            return null;
        }
    }

    @Override
    public UserDto findById(Long id) {
        try {
            User user =
                    userRepository.findById(id).orElseThrow(() ->
                            new NotFoundException(id.toString()));
            return userMapper.toUserDto(user);
        } catch (NotFoundException e) {
            log.error("User id = " + id + " not found");
            return null;
        }
    }

    @Override
    public boolean changePassword(ChangePasswordDto changePasswordDto) {
        JwtUserDetails jwtUser = getContextUser();
        if (passwordEncoder.matches(changePasswordDto.getOldPassword(),
                jwtUser.getPassword())) {

            userRepository.updatePassword(
                    passwordEncoder.encode(changePasswordDto.getNewPassword()),
                    jwtUser.getId());

            log.info("Password for user : {} change", jwtUser.getId());

            return true;
        }
        return false;
    }

    @Override
    public boolean delete(Long id) {
        if (userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
            log.info("User id " + id + " deleted");
            return true;
        }
        log.info("User id " + id + "not found");
        return false;
    }

    @Override
    public boolean deleteByUsername(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            userRepository.deleteByUsername(username);
            log.info("User delete");
            return true;
        }
        log.info("User not found");
        return false;
    }

    @Override
    public UserDto update(UserDto userDto) {
        Optional<User> user =
                userRepository.findByUsername(userDto.getUsername());
        if (!user.isPresent()) {
            return new UserDto();
        }
        userMapper.updateUser(user.get(), userDto);
        userRepository.save(user.get());
        return userMapper.toUserDto(user.get());
    }

    @Override
    public boolean changeMail(@NotNull UserMailDto userMailDto) {
        Optional<User> user =
                userRepository.findByUsername(userMailDto.getUsername());
        if (!user.isPresent()) {
            return false;
        }
        UserDto userDto = userMapper.toUserDto(user.get());
        userDto.setEmail(userMailDto.getEmail());
        userMapper.updateUser(user.get(), userDto);
        userRepository.save(user.get());
        return true;
    }

    @Override
    public boolean disable() {
        Optional<User> opUser = getUserByContext();
        if (opUser.isPresent()) {
            User user = opUser.get();
            user.setEnable(false);
            userRepository.save(user);
            log.info("User id {} disabled", user.getId());
            return true;
        }
        log.info("User not found");
        return false;
    }
}