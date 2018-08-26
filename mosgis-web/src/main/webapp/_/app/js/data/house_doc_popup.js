define ([], function () {

    $_DO.download_house_doc_popup = function (e) {    

        var box = $('#w2ui-popup')

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'house_docs', 
            id:     w2ui ['house_doc_popup_form'].record.id,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }


    $_DO.update_house_doc_popup = function (e) {
    
        var form = w2ui ['house_doc_popup_form']

        var r = form.record;

        if (!r.dt) {
            $('input[name=dt]').focus ()
            return alert ('Укажите, пожалуйста, дату документа');
        }
        
        if (!/^\d\d\.\d\d\.\d\d\d\d$/.test (r.dt)) {
            $('input[name=dt]').focus ()
            return alert ('Некорректная дата документа');
        }

        if (!r.no) {
            $('input[name=no]').focus ()
            return alert ('Укажите, пожалуйста, номер документа');
        }
        
        var r = form.values ()
        if (r.note == null) r.note = ''        
        
        query ({type: 'house_docs', action: 'edit', id: form.record.id}, {data: r}, reload_page)

    }

    return function (done) {
    
        var grid = w2ui ['house_docs_grid']
        
        var id = grid.getSelection () [0]
        
        var data = grid.get (id)
        
        var house = $('body').data ('data')
        
        var field = house.doc_fields.items.filter (function (r) {return r.id == data.id_type}) [0]
        
        data.dt = dt_dmy (house.item ['f_' + field.id_dt].substr (0, 10))
        data.no = house.item ['f_' + field.id_no]
    
        done (data)
        
    }
    
})