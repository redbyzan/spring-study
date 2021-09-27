package com.webservicestudy.webservicestudy.modules.account;


import com.querydsl.core.types.Predicate;
import com.webservicestudy.webservicestudy.modules.tag.Tag;
import com.webservicestudy.webservicestudy.modules.zone.Zone;

import java.util.Set;

public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QAccount account = QAccount.account;
        // account가 가지고 있는 zones중에 어떤 것이 zones에 들어 있고 account가 가지고 있는 tags중 어떤 것이 tags에 들어 있다면 account 리턴
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }

}





























