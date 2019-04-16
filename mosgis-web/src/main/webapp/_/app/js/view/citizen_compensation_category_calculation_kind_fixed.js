define ([], function () {
    
    var form_name = 'citizen_compensation_category_calculation_kind_fixed_form'

    return function (data, view) {

        fill (view, data.item, $(w2ui ['topmost_layout'].el('main')))

    }
    
})