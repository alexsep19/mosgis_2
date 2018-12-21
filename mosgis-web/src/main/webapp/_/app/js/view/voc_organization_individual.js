define ([], function () {

    return function (data, view) {

        $('title').text (data.item.label)

        data.item.vc_nsi_20 = data.vc_orgs_nsi_20
            .map (function (r) {return data.vc_nsi_20 [r.code]})
            .sort ()
            .join (',<br>')

        fill (view, data.item, $('#body'))

    }

})