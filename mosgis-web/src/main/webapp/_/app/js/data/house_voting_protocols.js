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

        w2ui ['topmost_layout'].unlock ('main')            

        data = $('body').data ('data')

        done(data);

    }

})