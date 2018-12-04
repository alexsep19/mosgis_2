define ([], function () {

    $_DO.create_voc_organization_legal_territories = function (e) {

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
            
        }, {}, w2ui [e.target].reload ())
    
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')            

        data = $('body').data ('data')

        done(data);

    }

})