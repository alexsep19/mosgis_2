define ([], function () {

    return function (data, view) {

        $('title').text (data.item.label)

        var it = data.item
        
        it.label_delegated = data.is_delegated ? 'Да' : 'Нет'        
        it.label_registered = it.isregistered ? 'Да' : 'Нет'
        it.label_actual = it.isactual ? 'Да' : 'Нет'
        
        data.item.vc_nsi_20 = data.vc_orgs_nsi_20
            .map (function (r) {return data.vc_nsi_20 [r.code]})
            .sort ()
            .join (',<br>')

        fill (view, data.item, $('#body'))

    }

})