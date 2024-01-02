package com.tongtech.proxy.core.utils;

import java.nio.charset.StandardCharsets;

public class StringMatcher {
    private static int tolower(int n) {
        if (n >= 'A' && n <= 'Z') {
            n = 'a' + n - 'A';
        }
        return n;
    }

    /* Glob-style pattern matching. */
    public static boolean stringmatchlen(byte[] pattern, int patOffset,
                                         byte[] string, int strOffset, boolean nocase) {
        int patternLen = pattern.length - patOffset;
        int stringLen = string.length - strOffset;

        try {
            while (patternLen > 0 && stringLen > 0) {
                switch (pattern[patOffset]) {
                    case '*':
                        while (patternLen > 1 && pattern[patOffset + 1] == '*') {
                            patOffset++;
                            patternLen--;
                        }
                        if (patternLen == 1) {
                            return true; /* match */
                        }
                        while (stringLen > 0) {
                            if (stringmatchlen(pattern, patOffset + 1,
                                    string, strOffset, nocase)) {
                                return true; /* match */
                            }
                            strOffset++;
                            stringLen--;
                        }
                        return false; /* no match */
//                break;
                    case '?':
                        if (stringLen == 0) {
                            return false; /* no match */
                        }
                        strOffset++;
                        stringLen--;
                        break;
                    case '[': {
                        boolean not, match;

                        patOffset++;
                        patternLen--;
                        not = pattern[patOffset] == '^';
                        if (not) {
                            patOffset++;
                            patternLen--;
                        }
                        match = false;
                        while (true) {
                            if (pattern[patOffset] == '\\' && patternLen >= 2) {
                                patOffset++;
                                patternLen--;
                                if (pattern[patOffset] == string[strOffset])
                                    match = true;
                            } else if (pattern[patOffset] == ']') {
                                break;
                            } else if (patternLen == 0) {
                                patOffset--;
                                patternLen++;
                                break;
                            } else if (pattern[patOffset + 1] == '-' && patternLen >= 3) {
                                int start = pattern[patOffset];
                                int end = pattern[patOffset + 2];
                                int c = string[0];
                                if (start > end) {
                                    int t = start;
                                    start = end;
                                    end = t;
                                }
                                if (nocase) {
                                    start = tolower(start);
                                    end = tolower(end);
                                    c = tolower(c);
                                }
                                patOffset += 2;
                                patternLen -= 2;
                                if (c >= start && c <= end)
                                    match = true;
                            } else {
                                if (!nocase) {
                                    if (pattern[0] == string[strOffset])
                                        match = true;
                                } else {
                                    if (tolower((int) pattern[patOffset]) == tolower((int) string[strOffset]))
                                        match = true;
                                }
                            }
                            patOffset++;
                            patternLen--;
                        }
                        if (not)
                            match = !match;
                        if (!match)
                            return false; /* no match */
                        strOffset++;
                        stringLen--;
                        break;
                    }
                    case '\\':
                        if (patternLen >= 2) {
                            patOffset++;
                            patternLen--;
                        }
                        /* fall through */
                    default:
                        if (!nocase) {
                            if (pattern[patOffset] != string[strOffset])
                                return false; /* no match */
                        } else {
                            if (tolower((int) pattern[patOffset]) != tolower((int) string[strOffset]))
                                return false; /* no match */
                        }
                        strOffset++;
                        stringLen--;
                        break;
                }
                patOffset++;
                patternLen--;
                if (stringLen == 0) {
                    while (patternLen > 0 && pattern[patOffset] == '*') {
                        patOffset++;
                        patternLen--;
                    }
                    break;
                }
            }
            if (patternLen == 0 && stringLen == 0) {
                return true;
            }
        } catch (Throwable t) {
        }
        return false;
    }

    public static void main(String[] arg) {
        byte[] str = "abcdef".getBytes(StandardCharsets.UTF_8);
        byte[] match = "ab*".getBytes(StandardCharsets.UTF_8);
        byte[] match2 = "ab*ef".getBytes(StandardCharsets.UTF_8);
        byte[] match3 = "ab*e".getBytes(StandardCharsets.UTF_8);
        byte[] match4 = "a?cde?".getBytes(StandardCharsets.UTF_8);
        byte[] match5 = "a[a-c]cd??".getBytes(StandardCharsets.UTF_8);

        System.out.println(stringmatchlen(match, 0, str, 0, false));
        System.out.println(stringmatchlen(match2, 0, str, 0, false));
        System.out.println(stringmatchlen(match3, 0, str, 0, false));
        System.out.println(stringmatchlen(match4, 0, str, 0, false));
        System.out.println(stringmatchlen(match5, 0, str, 0, false));
    }
}
