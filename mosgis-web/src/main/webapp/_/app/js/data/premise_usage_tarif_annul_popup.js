define ([], function () {

    $_DO.update_premise_usage_tarif_annul_popup = function (e) {

        var f = w2ui ['premise_usage_tarif_annul_popup_form']

        var v = f.values ()
        
        if (!v.cancelreason) die ('cancelreason', 'Укажите, пожалуйста, причину аннулирования')
        if (v.cancelreason.length > 4000) die ('cancelreason', 'Максимальная допустимая длина — 4000 символов')

        query ({type: 'premise_usage_tarifs', action: 'annul'}, {data: v}, reload_page)
            
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})