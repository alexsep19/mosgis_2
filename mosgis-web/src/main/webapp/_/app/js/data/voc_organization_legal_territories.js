define ([], function () {

    $_DO.create_voc_organization_legal_territories = function (e) {

        var ids = []
        w2ui [e.target].records.forEach ((element, i, arr) => {
            ids.push (element['oktmo_id'])
        })

        $_SESSION.set ('voc_oktmo_popup.ids', ids)

        $('body').data ('voc_oktmo_popup.callback', function (r) {

            if (!r) return

            query ({type: 'voc_organization_territories', id: undefined, part: 'create'}, {data: {oktmo: r.id, uuid_org: $_REQUEST.id}} , function (d) {

                w2ui [e.target].reload ()

            })

        })

        use.block ('voc_oktmo_popup')
    
    }

    $_DO.delete_voc_organization_legal_territories = function (e) {    

        if (!e.force) return
        
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'voc_organization_territories', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, function () {
            w2ui [e.target].remove (e.recid)
            w2ui [e.target].reload ()
        })
    
    }

    return function (done) {

        var layout = w2ui ['voc_organization_legal_layout']

        if (layout)
            layout.unlock('main')

        data = $('body').data ('data')

        done(data);

    }

})