define ([], function () {

    $_DO.update_house_voting_protocols_new = function (e) {

        var form = w2ui ['house_voting_protocols_new_form']

        var v = form.values ()
        
        if (!v.protocoldate) die ('protocoldate', 'Пожалуйста, введите дату составления протокола')
        if (!v.extravoting) die ('extravoting', 'Пожалуйста, укажите вид собрания')
        if (!v.meetingeligibility) die ('meetingeligibility', 'Пожалуйста, укажите правомочность собрания')
        if (!v.form_) die ('form_', 'Пожалуйста, выберите форму проведения')
        
        v['meetingeligibility'] = v['meetingeligibility'] == 0 ? "C" : "N"
        
        var tia = {type: 'voting_protocols'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['house_voting_protocols_grid']
        
        var data = clone ($('body').data ('data'))
        
        v['fiashouseguid'] = data.item.fiashouseguid

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу протокола?').yes (function () {openTab ('/voting_protocol/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})