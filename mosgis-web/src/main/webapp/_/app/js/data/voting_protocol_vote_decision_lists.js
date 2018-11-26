define ([], function () {  
    
    $_DO.create_voting_protocol_vote_decision_lists = function (e) {

        use.block ('voting_protocol_vote_decision_lists_new')
    
    }

    $_DO.delete_voting_protocol_vote_decision_lists = function (e) {    

        if (!e.force) return
        
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'vote_decision_lists', 
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