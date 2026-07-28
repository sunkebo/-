package com.company;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class GenPass {
    public static void main(String[] args) {
        String hash = new BCryptPasswordEncoder().encode(args[0]);
        System.out.println(hash);
    }
}
