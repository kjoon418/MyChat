package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.Blacklist;
import junwatson.mychat.domain.Member;
import junwatson.mychat.exception.MemberNotExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class BlacklistDao {

    public boolean isBlocked(Member member, Member target) {
        log.info("BlacklistDao.isBlocked() called");

        return target.getBlacklists().stream()
                .anyMatch(blacklist -> blacklist.getTargetMember().equals(member));
    }

    public boolean isBlacklistExists(Member member, Member target) {
        log.info("BlacklistDao.isBlacklistExists() called");

        return member.getBlacklists().stream()
                .anyMatch(blacklist -> blacklist.getTargetMember().equals(target));
    }

    public Blacklist createBlacklist(Member member, Member target) {
        log.info("BlacklistDao.createBlacklist() called");

        Blacklist blacklist = Blacklist.builder()
                .member(member)
                .targetMember(target)
                .build();
        member.getBlacklists().add(blacklist);
        target.getBlockedLists().add(blacklist);

        return blacklist;
    }

    public Blacklist removeBlacklist(Member member, Member target) {
        log.info("BlacklistDao.removeBlacklist() called");

        Blacklist findBlacklist = member.getBlacklists().stream()
                .filter(blacklist -> blacklist.getTargetMember().equals(target))
                .findAny()
                .orElseThrow(() -> new MemberNotExistsException("해당 회원이 차단 목록에 존재하지 않습니다."));
        member.getBlacklists().remove(findBlacklist);

        return findBlacklist;
    }
}
