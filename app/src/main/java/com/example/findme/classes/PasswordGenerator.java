package com.example.findme.classes;

import java.security.SecureRandom;
import java.util.Random;

public abstract class PasswordGenerator {
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_-+=<>?";

    public static String generatePassword() {
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder();

        // Ensure inclusion of at least one uppercase letter, one number, and one special character
        password.append(LETTERS.charAt(random.nextInt(26))); // Add one random uppercase letter
        password.append(LETTERS.charAt(26 + random.nextInt(26))); // Add one random lowercase letter
        password.append(NUMBERS.charAt(random.nextInt(10))); // Add one random number
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length()))); // Add one random special character

        // Fill remaining characters to make the password 8 characters long
        for (int i = 0; i < 4; i++) {
            password.append(randomChar());
        }

        // Shuffle the characters in the password
        String shuffledPassword = shuffleString(password.toString());

        return shuffledPassword;
    }

    // Helper method to shuffle the characters in a string
    private static String shuffleString(String string) {
        char[] characters = string.toCharArray();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = random.nextInt(characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }

    // Helper method to generate a random character
    private static char randomChar() {
        String combinedChars = LETTERS + NUMBERS + SPECIAL_CHARS;
        SecureRandom random = new SecureRandom();
        return combinedChars.charAt(random.nextInt(combinedChars.length()));
    }

    public static String generateNumericPassword(int length) {
        String allowedChars = "0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        // Generate the password by appending random characters from allowedChars
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allowedChars.length());
            password.append(allowedChars.charAt(randomIndex));
        }

        return password.toString();
    }
}
