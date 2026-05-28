package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.entity.Mention;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.repository.MentionRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentionService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\w.-]+)", Pattern.CASE_INSENSITIVE);

    private final MentionRepository mentionRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<User> syncMentions(Long commentId, String content) {
        mentionRepository.deleteAllByCommentId(commentId);

        Set<String> mentionedUsernames = extractUsernames(content);
        List<User> mentionedUsers = new ArrayList<>();

        for (String username : mentionedUsernames) {
            userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
                mentionRepository.save(Mention.builder()
                        .commentId(commentId)
                        .userId(user.getId())
                        .build());
                mentionedUsers.add(user);
            });
        }
        return mentionedUsers;
    }

    public List<Long> getCommentIdsByUserId(Long userId) {
        return mentionRepository.findCommentIdsByUserId(userId);
    }

    public List<User> getMentionedUsers(Long commentId) {
        List<Mention> mentions = mentionRepository.findAllByCommentId(commentId);
        return mentions.stream()
                .map(m -> userRepository.findById(m.getUserId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Set<String> extractUsernames(String content) {
        Set<String> usernames = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            usernames.add(matcher.group(1).toLowerCase());
        }
        return usernames;
    }
}
