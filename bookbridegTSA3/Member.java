
package bookbridge.model;

public abstract class Member {

    private final String memberId;
    private final String memberName;

    public Member(String memberId, String memberName) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException(
                    "Member ID cannot be blank.");
        }

        if (memberName == null || memberName.isBlank()) {
            throw new IllegalArgumentException(
                    "Member name cannot be blank.");
        }

        this.memberId = memberId;
        this.memberName = memberName;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public abstract int getMaximumLoanDays();

    public abstract double getDailyFine();
}
