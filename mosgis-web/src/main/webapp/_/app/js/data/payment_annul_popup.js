define ([], function () {

    $_DO.update_payment_annul_popup = function (e) {

        var f = w2ui ['payment_annul_popup_form']

        var v = f.values ()
        
        if (!v.cancellationdate) die ('cancellationdate', 'Укажите, пожалуйста, дату аннулирования')
        if (!v.cancellationcomment) die ('cancellationcomment', 'Укажите, пожалуйста, причину аннулирования')
        if (v.cancellationcomment.length > 210) die ('cancellationcomment', 'Максимальная допустимая длина — 210 символов')

        query ({type: 'payments', action: 'annul'}, {data: v}, reload_page)
            
    }

    return function (done) {
    
        var data = clone ($('body').data ('data'))

        data.record = {
            cancellationcomment: data.item.cancellationcomment,
            cancellationdate: dt_dmy(data.item.cancellationdate || new Date().toJSON())
        }

        done(data)
        
    }
    
})