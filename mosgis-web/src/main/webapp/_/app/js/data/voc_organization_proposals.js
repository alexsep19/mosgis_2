define ([], function () {

    $_DO.add_branch_voc_organization_proposals = function (record) {

        var record = $_SESSION.delete('record') || {}

        record.id_type = 2

        if (record.uuid) {
            record.uuid_org_parent = record.uuid,
            record.label_org_parent = record.label
        }

        $_SESSION.set('record', record)

        use.block('voc_organization_branch_add_new')
    }

    $_DO.add_alien_voc_organization_proposals = function (record) {

        var record = $_SESSION.delete('record') || {}

        record.id_type = 3

        $_SESSION.set('record', record)

        use.block('voc_organization_proposal_alien_add_new')
    }

    return function (done) {

        var layout = w2ui ['rosters_layout']

        if (layout) layout.unlock ('main')

        query({type: 'voc_organization_proposals', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies(data, data)

            done(data)

        })

    }

})