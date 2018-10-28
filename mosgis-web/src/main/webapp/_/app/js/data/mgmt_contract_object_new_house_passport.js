define ([], function () {

    $_DO.create_mgmt_contract_object_house_passport = function (e) {

        var form = w2ui ['mgmt_contract_object_new_house_passport_form']

        var v = form.values ()
        
        if (!v.is_condo) die ('type', 'Пожалуйста, укажите тип дома')
        
        var tia = {type: 'houses'}
        tia.id = form.record.id
        tia.action = 'create'
        
        var done = reload_page

        var data = clone ($('body').data ('data'))
        
        console.log (data)
        
        var new_data = {'type': v.is_condo, "fiashouseguid": data.item.fiashouseguid, "address": data.item ["fias.label"]}

        query (tia, {data: new_data}, function (data) {
        
            w2popup.close ()
            
            //if (data.id) w2confirm ('Перейти на страницу паспорта дома?').yes (function () {openTab ('/houses/' + data.id)})
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})