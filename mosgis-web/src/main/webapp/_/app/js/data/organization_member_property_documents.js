define ([], function () {

    $_DO.create_person_organization_member_property_documents = function (e) {

        var data = clone($('body').data('data'))

        $_SESSION.set('record', {
            prc: 100,
            uuid_person_owner: data.item.uuid_person_member,
            label_person_owner: data.item["person.label"]
        })

        use.block ('property_document_house_person_new')

    }

    $_DO.create_org_organization_member_property_documents = function (e) {

        var data = clone($('body').data ('data'))

        $_SESSION.set ('record', {
            prc: 100,
            uuid_org_owner: data.item.uuid_org_member,
            label_org_owner: data.item["org.label"]
        })

        use.block ('property_document_house_org_new')

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var org_member = clone($('body').data ('data'))

        done (org_member);

    }

})