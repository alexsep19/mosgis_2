define ([], function () {

    $_DO.create_voc_organization_legal_territories = function (e) {

        //use.block ('voting_protocol_vote_decision_lists_new')
    
    }

    $_DO.delete_voc_organization_legal_territories = function (e) {    

        if (!e.force) return
        
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'voc_organization_legal_territories', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')            

        data = $('body').data ('data')

        done(data);

    }

})