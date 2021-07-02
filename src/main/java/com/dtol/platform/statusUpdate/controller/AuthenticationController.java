package com.dtol.platform.statusUpdate.controller;

import com.dtol.platform.statusUpdate.model.AuthenticationRequest;
import com.dtol.platform.statusUpdate.model.AuthenticationResponse;
import com.dtol.platform.statusUpdate.model.MyUserDetails;
import com.dtol.platform.statusUpdate.model.User;
import com.dtol.platform.statusUpdate.repository.UserRepository;
import com.dtol.platform.statusUpdate.service.Impl.JPAUserDetailsService;
import com.dtol.platform.statusUpdate.service.Impl.StatusUpdateServiceImpl;
import com.dtol.platform.statusUpdate.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/v1")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private JPAUserDetailsService userDetailsService;

    @Autowired
    StatusUpdateServiceImpl statusUpdateService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/register")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void register() {
        User user = new User();
        user.setActive(true);
        user.setUsername("dtol_status");
        user.setPassword(passwordEncoder.encode("trackingStatusUpdate@APIv1"));
        user.setRoles("ROLE_USER");
        userRepository.save(user);
    }

    @RequestMapping(value = "/auth/jwt", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        HttpHeaders responseHeaders = new HttpHeaders();
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );

        final MyUserDetails userDetails = (MyUserDetails) userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        AuthenticationResponse response = new AuthenticationResponse(jwt);

        return new ResponseEntity<>(response, responseHeaders, HttpStatus.OK);

    }

    @RequestMapping(value = "/update_status", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateTrackingStatus(@RequestPart("file") MultipartFile file) throws Exception {
        String response = statusUpdateService.updateOrganismTrackingStatus(file);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}