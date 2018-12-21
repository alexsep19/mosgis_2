define ([], function () {

    $_DO.create_person_organization_member_property_documents = function (e) {

        use.block ('property_document_person_new')

    }

    $_DO.create_org_organization_member_property_documents = function (e) {

        use.block ('property_document_org_new')

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var org_member = clone($('body').data ('data'))

        done (org_member);

    }

})