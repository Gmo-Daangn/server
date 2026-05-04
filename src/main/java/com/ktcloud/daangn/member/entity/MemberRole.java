package com.ktcloud.daangn.member.entity;

public enum MemberRole {
    MEMBER, ADMIN;

    @Override
    public String toString() {
        return "ROLE_"+ name();
    }
}
