define ([], function () {

    $_DO.update_tarif_coeffs_popup = function (e) {

        var form = w2ui ['tarif_coeff_form']

        var v = form.values ()

        if (!v.coefficientvalue || !(0 <= v.coefficientvalue && v.coefficientvalue < 1000))
            die('coefficientvalue', 'Укажите, пожалуйста, значение коэффициента от 0 до 999.999')

        if (!v.coefficientdescription) die('coefficientdescription', 'Укажите, пожалуйста, описание коэффициента')

        var tia = {type: 'tarif_coeffs'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var grid = w2ui ['tarif_coeffs_grid']

        query (tia, {data: v}, function () {
        
            w2popup.close ()
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete ('record') || {}

        data._can = data.record._can = {
            update: 1
        }

        done(data)

    }

})