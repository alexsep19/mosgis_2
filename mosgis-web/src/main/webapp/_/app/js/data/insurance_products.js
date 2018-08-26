define ([], function () {

    function setDeleted (action) {

        var grid = w2ui ['insurance_products_grid']
        
        var id = grid.getSelection () [0]

        var tia = {
            type:   'insurance_products', 
            id:     id,
            action: action
        }

        query (tia, {}, function () {
            grid.reload (function () {
                grid.refresh ()            
                grid.selectNone ()
                grid.select (id)
            })
        })

    }

    $_DO.delete_insurance_products = function () {
        if (confirm ('Удалить эту запись?')) setDeleted ('delete')
    }
    
    $_DO.undelete_insurance_products = function (e) {
        if (confirm ('Восстановить эту запись?')) setDeleted ('undelete')
    }

    $_DO.create_insurance_products = function (e) {       
        $_SESSION.set ('record', {})
        use.block ('insurance_product_popup')
    }

    $_DO.edit_insurance_products = function (e) {       

        var grid = w2ui ['insurance_products_grid']
        
        var r = grid.get (grid.getSelection () [0])
        
        if (r.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        $_SESSION.set ('record', r)

        use.block ('insurance_product_popup')

    }
    
    $_DO.download_insurance_products = function (e) {    
    
        var box = $('body')

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'insurance_products', 
            id:     e.recid,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }
    
    return function (done) {
    
        var layout = w2ui ['vocs_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'insurance_products', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 

    }

})