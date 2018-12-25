define ([], function () {

    $_DO.update_public_property_contract_annul_popup = function (e) {

        var f = w2ui ['public_property_contract_annul_popup_form']

        var v = f.values ()
        
        if (!v.reasonofannulment) die ('reasonofannulment', 'Укажите, пожалуйста, причину аннулирования')
        if (v.reasonofannulment.length > 1000) die ('reasonofannulment', 'Максимальная допустимая длина — 1000 символов')

        query ({type: 'public_property_contracts', action: 'annul'}, {data: v}, reload_page)
            
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})