define ([], function () {
/*
    $_DO.edit_house_contracts = function (e) {
            
        use.block ('house_doc_popup')
    
    }    
    
    $_DO.create_house_contracts = function (e) {
            
        if ($('body').data ('data').doc_fields.items.filter (not_off).length == 0) return alert ('Документы всех типов, доступных для этого дома, уже добавлены')

        use.block ('house_doc_new')
    
    }
    
    $_DO.download_house_contracts = function (e) {    
    
        var box = $('body')

        var r = this.get (e.recid)

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'house_contracts', 
            id:     e.recid,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }

    $_DO.delete_house_contracts = function (e) {    
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'house_contracts', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }
*/
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = $('body').data ('data')
  
        query ({type: 'houses', part: 'contracts'}, {}, function (d) {
        
            var lines = []
        
            for (var k in d) {
            
                $.each (d [k], function () {                    
                    this.type = k
                    this.recid = this.type + '/' + this.id
                    lines.push (this)                
                })
            
            }
            
            data.lines = lines
        
darn (data)            
        
            done (data)            
        
        }) 
  
    
    }

})