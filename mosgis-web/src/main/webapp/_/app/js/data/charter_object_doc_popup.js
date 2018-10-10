define ([], function () {

    $_DO.download_charter_object_doc_popup = function (e) {    

        var box = $('#w2ui-popup')

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'charter_docs', 
            id:     w2ui ['charter_object_doc_popup_form'].record.id,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }

    $_DO.update_charter_object_doc_popup = function (e) {
    
        var form = w2ui ['charter_object_doc_popup_form']
        
        var r = form.values ()
        
        query ({type: 'charter_docs', action: 'edit', id: form.record.id}, {data: r}, reload_page)

    }

    return function (done) {

        var grid = w2ui ['charter_object_docs_grid']

        var id = grid.getSelection () [0]

        var data = grid.get (id)

        done (data)

    }

})