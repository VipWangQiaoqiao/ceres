package com.bc.ceres.binio.expr;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.Type;

import java.io.IOException;

public class CompoundExpr extends AbstractExpression {
    private final String name;
    private final Member[] members;
    private final boolean constant;

    public CompoundExpr(String name, Member[] members) {
        this.name = name;
        this.members = members;
        for (Member member : members) {
            member.type.setParent(this);
        }
        constant = isConstant(members);
    }

    public boolean isConstant() {
        return constant;
    }

    public Object evaluate(CompoundData context) throws IOException {
        // todo - wrong child parent used here, parent for children must be instance of "this" compound
        com.bc.ceres.binio.CompoundType.Member[] typeMembers = new CompoundType.Member[members.length];
        for (int i = 0; i < members.length; i++) {
            Member member = members[i];
            final Type memberType = (Type) member.type.evaluate(context);
            typeMembers[i] = new CompoundType.Member(member.name, memberType);
        }
        return new CompoundType(name, typeMembers);
    }

    public static boolean isConstant(Member[] members) {
        for (Member member : members) {
            if (!member.type.isConstant()) {
                return false;
            }
        }
        return true;
    }

    public static class Member {
        private final String name;
        private final Expression type;

        public Member(String name, Expression type) {
            this.name = name;
            this.type = type;
        }
    }
}