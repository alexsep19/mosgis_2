define ([], function () {

    $_DO.update_settlement_doc_annul_popup = function (e) {

        var f = w2ui ['settlement_doc_annul_popup_form']

        var v = f.values ()
        
        if (!v.reasonofannulment) die ('reasonofannulment', 'Укажите, пожалуйста, причину аннулирования')
        if (v.reasonofannulment.length > 1000) die ('reasonofannulment', 'Максимальная допустимая длина — 1000 символов')

        query ({type: 'settlement_docs', action: 'annul'}, {data: v}, reload_page)
            
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})