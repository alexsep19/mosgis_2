define ([], function () {

    $_DO.create_person_voc_organization_legal_members = function (e) {

        $_SESSION.set('record', {uuid_org: $_REQUEST.id})

        use.block('org_member_document_person_new')
    }

    $_DO.create_org_voc_organization_legal_members = function (e) {

        $_SESSION.set('record', {uuid_org: $_REQUEST.id})

        use.block('org_member_document_org_new')
    }

    $_DO.delete_voc_organization_legal_members = function (e) {

        if (!e.force)
            return

        $('.w2ui-message').remove()

        e.preventDefault()

        query({

            type: 'organization_members',
            id: w2ui [e.target].getSelection() [0],
            action: 'delete',

        }, {}, reload_page)

    }

    return function (done) {

        var layout = w2ui ['rosters_layout']

        if (layout) layout.unlock ('main')

        done({})
    }

})