define ([], function () {

    $_DO.edit_house_voting_protocols = function (e) {
            
        //use.block ('house_voting_protocol_popup')
    
    }    
    
    $_DO.create_house_voting_protocols = function (e) {

        use.block ('house_voting_protocols_new')
    
    }

    $_DO.delete_house_voting_protocols = function (e) {    
    
        if (!e.force) return
        
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'voting_protocols', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {
    
        var data = $('body').data ('data')

        query ({type: 'voting_protocols', part: 'vocs', id: undefined}, {}, function (d) {
        
            add_vocabularies (d, d)

            w2ui ['topmost_layout'].unlock ('main')
            
            data.vc_gis_status = d.vc_gis_status

            done (data)
        
        })

    }

})