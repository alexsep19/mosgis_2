define ([], function () {  
    
    $_DO.create_owner_voting_protocol_vote_initiators = function (e) {

        use.block ('voting_protocol_vote_initiators_new_owner')
    
    }

    $_DO.create_org_voting_protocol_vote_initiators = function (e) {

        use.block ('voting_protocol_vote_initiators_new_org')
    
    }

    $_DO.delete_voting_protocol_vote_initiators = function (e) {    

        if (!e.force) return
        
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'vote_initiators', 
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