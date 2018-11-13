define ([], function () {

    $_DO.update_working_list_common_items_popup = function (e) {
    
        var g = w2ui ['new_org_works_grid']
        
        var v = {ids: g.getSelection ()}
        
        if (!v.ids.length) die ('foo', 'Вы не выбрали ни одной работы')
        
        query ({type: 'working_lists', action: 'add_items'}, {data: v}, function () {
        
            w2popup.close ()

            use.block ('working_list_common_items')
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
                
        data.record = {}
        
        done (data)

    }

})