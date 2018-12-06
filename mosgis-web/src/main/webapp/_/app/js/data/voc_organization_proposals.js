define ([], function () {

    $_DO.add_branch_voc_organization_proposals = function (record) {

        var record = $_SESSION.delete('record') || {}

        record.id_type = 1

        if (record.uuid) {
            record.uuid_org_parent = record.uuid,
            record.label_org_parent = record.label
        }

        $_SESSION.set('record', record)

        use.block('voc_organization_branch_add_new')
    }

    return function (done) {

        var layout = w2ui ['rosters_layout']

        if (layout) layout.unlock ('main')

        done({})

    }

})