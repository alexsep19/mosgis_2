define ([], function () {

    $_DO.edit_voting_protocol_docs = function (e) {
            
        use.block ('voting_protocol_docs_popup')
    
    }    
    
    $_DO.create_voting_protocol_docs = function (e) {
            
        use.block ('voting_protocol_docs_new')
    
    }
    
    $_DO.download_voting_protocol_docs = function (e) {    
    
        var box = $('body')

        var r = this.get (e.recid)

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'voting_protocol_docs', 
            id:     e.recid,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }

    $_DO.delete_voting_protocol_docs = function (e) {    
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'voting_protocol_docs', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = clone ($('body').data ('data'))

        done (data)

    }

})