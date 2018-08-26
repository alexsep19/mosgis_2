define ([], function () {

    $_DO.close_passport_multiple_popup = function (e) {
        w2popup.close ()    
    }
        
    $_DO.update_passport_multiple_popup = function (e) {    

        w2popup.lock ('Сохраняем данные...', true)

        var data = {ids: [], code: $_SESSION.get ('field').def.id}

        for (var id in $_SESSION.get ('ids')) if (id > 0) data.ids.push (parseInt (id))

        query ({
            type: getPluralType (), 
            id: $('body').data ('data').item.uuid,
            action: 'set_multiple'
        }, {
        
            data: data
            
        }, reload_page)
        
    }        

    return function (done) {

        var data = $_SESSION.get ('field')

        $.each (data.voc.items, function () {this.recid = this.id})

        $_SESSION.set ('ids', data.ids)

        done (data)

    }
    
})