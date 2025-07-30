package com.example.owlpost_2_0.ChatRoom;

import java.util.*;

public class GroupChat {
    private String groupId;
    private String groupName;
    private String groupDescription;
    private String creatorUsername;
    private Set<String> members;
    private String groupImagePath;
    private Date createdAt;
    private Date lastActivity;
    private boolean isActive;

    public GroupChat() {
        this.members = new HashSet<>();
        this.createdAt = new Date();
        this.lastActivity = new Date();
        this.isActive = true;
    }

    public GroupChat(String groupId, String groupName, String creatorUsername) {
        this();
        this.groupId = groupId;
        this.groupName = groupName;
        this.creatorUsername = creatorUsername;
        this.members.add(creatorUsername); // Creator is automatically a member
    }

    public GroupChat(String groupId, String groupName, String groupDescription, String creatorUsername) {
        this(groupId, groupName, creatorUsername);
        this.groupDescription = groupDescription;
    }

    // Getters and Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupDescription() { return groupDescription; }
    public void setGroupDescription(String groupDescription) { this.groupDescription = groupDescription; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    public Set<String> getMembers() { return new HashSet<>(members); }
    public void setMembers(Set<String> members) { this.members = new HashSet<>(members); }

    public String getGroupImagePath() { return groupImagePath; }
    public void setGroupImagePath(String groupImagePath) { this.groupImagePath = groupImagePath; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getLastActivity() { return lastActivity; }
    public void setLastActivity(Date lastActivity) { this.lastActivity = lastActivity; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean addMember(String username) {
        if (username != null && !username.trim().isEmpty()) {
            return members.add(username);
        }
        return false;
    }

    public boolean removeMember(String username) {
        if (username.equals(creatorUsername)) {
            return false;
        }
        return members.remove(username);
    }

    public boolean isMember(String username) {
        return members.contains(username);
    }

    public boolean isCreator(String username) {
        return creatorUsername.equals(username);
    }

    public int getMemberCount() {
        return members.size();
    }

    public List<String> getMembersList() {
        return new ArrayList<>(members);
    }

    public void updateLastActivity() {
        this.lastActivity = new Date();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GroupChat groupChat = (GroupChat) obj;
        return Objects.equals(groupId, groupChat.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }

    @Override
    public String toString() {
        return "GroupChat{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", creatorUsername='" + creatorUsername + '\'' +
                ", memberCount=" + members.size() +
                ", isActive=" + isActive +
                '}';
    }
}