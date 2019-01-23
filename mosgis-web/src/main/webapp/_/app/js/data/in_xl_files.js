define ([], function () {

    $_DO.download_errors_in_xl_files = function (e) {
    
        var box = $('body')

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({
            type:   'in_xl_files', 
            id:     e.recid,
            action: 'download_errors',
        }, {}, {
            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},
            onload: function () {w2utils.unlock (box)},
        })
        
    
    }

    $_DO.download_in_xl_files = function (e) {

        var box = $('body')

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({
            type:   'in_xl_files', 
            id:     e.recid,
            action: 'download',
        }, {}, {
            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},
            onload: function () {w2utils.unlock (box)},
        })
    
    }

    return function (done) {        
        
        var layout = w2ui ['integration_layout']
            
        if (layout) layout.unlock ('main')
                
        query ({type: 'in_xl_files', part: 'vocs'}, {}, function (data) {
        
            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        })

    }

})